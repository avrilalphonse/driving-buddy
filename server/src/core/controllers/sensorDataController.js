import fs from 'fs';
import csv from 'csv-parser';
import SensorData from '../models/sensorDataModel.js';

async function importCsvToMongoDB(csvPath) {
  const results = [];
  return new Promise((resolve, reject) => {
    fs.createReadStream(csvPath)
      .pipe(csv())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        const docs = results.map(row => {
          const speed = parseFloat(row.Speed) || 0;
          const acceleration = parseFloat(row.Acceleration) || 0;
          const rpm = parseFloat(row.RPM) || 0;
          const engine_load = parseFloat(row['Engine Load']) || 0;
          // const steering_angle_deg = parseFloat(row['Steering Angle']);
          const timeStr = row.Timestamp?.trim();
          // flags (independent)
          const hard_braking = acceleration <= -3.0;
          // const sharp_turn = Math.abs(steering_angle_deg) >= 25;
          const inconsistent_speed = acceleration >= 3.0 && rpm >= 3500;
          // const lane_deviation = ...; // uncomment when logic figured out

          return {
            timestamp: timeStr,
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

export const getSimpleCounts = async (req, res) => {
    try {
        const hardBrakingCount = await SensorData.countDocuments({ hard_braking: true });
        const inconsistentSpeedCount = await SensorData.countDocuments({ inconsistent_speed: true });
        
        res.json({
            hard_braking_count: hardBrakingCount,
            inconsistent_speed_count: inconsistentSpeedCount
        });
        
    } catch (error) {
        console.error('Error getting counts:', error);
        res.status(500).json({ error: 'Failed to get counts' });
    }
};

export const debugHardBraking = async (req, res) => {
    try {
        const hardBrakingExamples = await SensorData.find({ hard_braking: true })
            .select('timestamp acceleration speed rpm hard_braking')
            .sort({ timestamp: 1 })
            .limit(20)
            .lean();
        
        const veryNegativeCount = await SensorData.countDocuments({ 
            acceleration: { $lte: -3.0 } 
        });
        
        const hardBrakingByTime = await SensorData.find({ hard_braking: true })
            .select('timestamp acceleration')
            .sort({ timestamp: 1 })
            .lean();
        
        res.json({
            hard_braking_examples: hardBrakingExamples,
            acceleration_lte_minus3: veryNegativeCount,
            total_hard_braking_flag: await SensorData.countDocuments({ hard_braking: true }),
            hard_braking_by_time: hardBrakingByTime
        });
        
    } catch (error) {
        res.status(500).json({ error: 'Failed to debug', details: error.message });
    }
};

export const clearAllData = async (req, res) => {
    try {
        const result = await SensorData.deleteMany({});
        res.json({ 
            message: 'All data cleared', 
            deleted_count: result.deletedCount 
        });
    } catch (error) {
        res.status(500).json({ error: 'Failed to clear data', details: error.message });
    }
};

export { importCsvToMongoDB };
