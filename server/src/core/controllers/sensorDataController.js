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

export { importCsvToMongoDB };

