import fs from 'fs';
import csv from 'csv-parser';
import SensorData from '../models/sensorDataModel.js';
import PersistentSummaryData from '../models/persistentSummaryDataModel.js';

async function importCsvToMongoDB(csvPath) {
  const results = [];
  return new Promise((resolve, reject) => {
    fs.createReadStream(csvPath)
      .pipe(csv())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        const docs = results.map(row => {
          const speed = parseFloat(row.Speed);
          const acceleration = parseFloat(row.Acceleration);
          const rpm = parseFloat(row.RPM);
          const engine_load = parseFloat(row['Engine Load']);
          // const steering_angle_deg = parseFloat(row['Steering Angle']);

          // flags (independent)
          const hard_braking = acceleration <= -3.0;
          // const sharp_turn = Math.abs(steering_angle_deg) >= 25;
          const inconsistent_speed = acceleration >= 3.0 && rpm >= 3500;
          // const lane_deviation = ...; // uncomment when logic figured out

          return {
            speed,
            acceleration,
            rpm,
            engine_load,
            // steering_angle_deg, 
            hard_braking,
            // sharp_turn,
            inconsistent_speed
            // lane_deviation
          };
        });
        await SensorData.insertMany(docs);
        resolve();
      })
      .on('error', reject);
  });
}

// a simple helper function to group incidents within 5 seconds
function consolidateIncidents(incidents) {
    if (!incidents || incidents.length === 0) return [];
    
    const consolidated = [];
    let i = 0;
    
    while (i < incidents.length) {
        const startIncident = incidents[i];
        let j = i + 1;
        
        // find all incidents within 5 seconds of the start
        while (j < incidents.length) {
            const timeDiff = new Date(incidents[j].timestamp) - new Date(startIncident.timestamp);
            if (timeDiff <= 5000) {
                j++;
            } else {
                break;
            }
        }
        
        // group found, add as one consolidated incident
        consolidated.push(startIncident);
        i = j; // move to next group
    }
    
    return consolidated;
}

// helper function for day suffixes!
function getDaySuffix(day) {
    if (day >= 11 && day <= 13) {
        return 'th';
    }
    switch (day % 10) {
        case 1: return 'st';
        case 2: return 'nd';
        case 3: return 'rd';
        default: return 'th';
    }
}

export const getBucketedData = async (req, res) => {
    try {
        const { windowSeconds = 10 } = req.query;

        console.log('Fetching data separated by drives...');
        
        // Get all unique tripIDs (drives)
        const tripIDs = await SensorData.distinct('tripID');
        console.log(`Found ${tripIDs.length} drives:`, tripIDs);
        
        const driveData = [];
        
        for (const tripID of tripIDs) {
            // Get hard braking incidents for this drive
            const hardBrakingData = await SensorData.find({ 
                tripID: tripID,
                hard_braking: true 
            }).sort({ timestamp: 1 }).lean();
            
            // Get inconsistent speed incidents for this drive
            const inconsistentSpeedData = await SensorData.find({ 
                tripID: tripID,
                inconsistent_speed: true 
            }).sort({ timestamp: 1 }).lean();
            
            // Get lane deviation incidents for this drive (when lane_offset_direction is left or right, not centre)
            let laneDeviationData = await SensorData.find({ 
                tripID: tripID,
                lane_offset_direction: { 
                    $exists: true, 
                    $ne: null,
                    $nin: [" centre", "centre", " center", "center"] // Exclude centre/center variations
                }
            }).sort({ timestamp: 1 }).lean();
            
            // group continuous incidents within 5 seconds
            const consolidatedHardBraking = consolidateIncidents(hardBrakingData);
            const consolidatedInconsistentSpeed = consolidateIncidents(inconsistentSpeedData);
            const consolidatedLaneDeviation = consolidateIncidents(laneDeviationData);
            
            // Check if lane deviation count is above 50, then set count to 0
            const originalLaneDeviationCount = consolidatedLaneDeviation.length;
            let laneDeviationCount = originalLaneDeviationCount;
            if (originalLaneDeviationCount > 50) {
                laneDeviationCount = 0;
            }
            
            // Get trip date from tripID (format: DDMMYYHHMMSS)
            const dateStr = tripID.substring(0, 6);
            const day = parseInt(dateStr.substring(0, 2));
            const month = parseInt(dateStr.substring(2, 4));
            const year = 2000 + parseInt(dateStr.substring(4, 6));
            const driveDate = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`;
            
            driveData.push({
                tripID: tripID,
                date: driveDate,
                displayDate: `July ${day}${getDaySuffix(day)}, 2025 Drive`,
                incidents: {
                    sudden_braking: consolidatedHardBraking.length,
                    inconsistent_speed: consolidatedInconsistentSpeed.length,
                    lane_deviation: laneDeviationCount
                },
                raw_data: {
                    sudden_braking: hardBrakingData,
                    inconsistent_speed: inconsistentSpeedData,
                    lane_deviation: laneDeviationData
                }
            });
            
            console.log(`Drive ${tripID} (${driveDate}): ${consolidatedHardBraking.length} hard braking, ${consolidatedInconsistentSpeed.length} inconsistent speed, ${laneDeviationCount} lane deviations`);
        }
        
        // Sort drives by date (newest first)
        driveData.sort((a, b) => b.tripID.localeCompare(a.tripID));
        
        // Calculate totals
        const totals = {
            sudden_braking: driveData.reduce((sum, drive) => sum + drive.incidents.sudden_braking, 0),
            inconsistent_speed: driveData.reduce((sum, drive) => sum + drive.incidents.inconsistent_speed, 0),
            lane_deviation: driveData.reduce((sum, drive) => sum + drive.incidents.lane_deviation, 0)
        };
        totals.total = totals.sudden_braking + totals.inconsistent_speed + totals.lane_deviation;
        
        res.json({
            drives: driveData,
            summary: {
                total_drives: driveData.length,
                ...totals
            }
        });
        
    } catch (error) {
        console.error('Error fetching drive data:', error);
        res.status(500).json({ error: 'Failed to fetch drive data', details: error.message });
    }
};
export { importCsvToMongoDB };

export const getPersistentSummaryData = async (req, res) => {
    try {
        console.log('Fetching data from persistent_summary_data collection...');
        const { userID } = req.query;
        if (!userID) {
            return res.status(400).json({ error: 'userID query parameter is required' });
        }

        // Get all trips, sorted by start time (newest first)
        const trips = await PersistentSummaryData.find({
            $or: [
                { userID },
                { userID: { $exists: false } }
            ]
        })
            .sort({ start_of_trip_timestamp: -1 })
            .lean();

        console.log(`Found ${trips.length} trips`);

        // Format with full trip details
        const driveData = trips.map(trip => {
            const date = new Date(trip.start_of_trip_timestamp);
            const formattedDate = date.toISOString().split('T')[0];

            // Calculate trip duration in minutes
            const duration = (new Date(trip.end_of_trip_timestamp) - new Date(trip.start_of_trip_timestamp)) / 1000 / 60;

            return {
                date: formattedDate,
                tripID: trip.tripID,
                start_timestamp: trip.start_of_trip_timestamp,
                end_timestamp: trip.end_of_trip_timestamp,
                duration_minutes: Math.round(duration),
                start_location: trip.start_of_trip_location,  // [lon, lat]
                end_location: trip.end_of_trip_location,      // [lon, lat]
                incidents: {
                    sudden_braking: trip.hard_braking?.length || 0,
                    inconsistent_speed: trip.inconsistent_speed?.length || 0,
                    lane_deviation: trip.lane_deviation?.length || 0,
                    sharp_turn: trip.sharp_turning?.length || 0
                },
                // Detailed incidents with location and timestamp
                incident_details: {
                    hard_braking: trip.hard_braking?.map(incident => ({
                        latitude: incident[0],
                        longitude: incident[1],
                        timestamp: incident[2]
                    })) || [],
                    inconsistent_speed: trip.inconsistent_speed?.map(incident => ({
                        latitude: incident[0],
                        longitude: incident[1],
                        timestamp: incident[2]
                    })) || [],
                    lane_deviation: trip.lane_deviation?.map(incident => ({
                        latitude: incident[0],
                        longitude: incident[1],
                        timestamp: incident[2]
                    })) || [],
                    sharp_turning: trip.sharp_turning?.map(incident => ({
                        latitude: incident[0],
                        longitude: incident[1],
                        timestamp: incident[2]
                    })) || []
                }
            };
        });

        const totals = {
            sudden_braking: driveData.reduce((sum, drive) => sum + drive.incidents.sudden_braking, 0),
            inconsistent_speed: driveData.reduce((sum, drive) => sum + drive.incidents.inconsistent_speed, 0),
            lane_deviation: driveData.reduce((sum, drive) => sum + drive.incidents.lane_deviation, 0),
            sharp_turn: driveData.reduce((sum, drive) => sum + drive.incidents.sharp_turn, 0)
        };
        totals.total = totals.sudden_braking + totals.inconsistent_speed + totals.lane_deviation + totals.sharp_turn;

        res.json({
            drives: driveData,
            summary: {
                total_drives: driveData.length,
                ...totals
            }
        });

    } catch (error) {
        console.error('Error fetching persistent summary data:', error);
        res.status(500).json({ error: 'Failed to fetch persistent summary data', details: error.message });
    }
};
