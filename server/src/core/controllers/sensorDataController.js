const fs = require('fs');
const csv = require('csv-parser');
const SensorData = require('../models/sensorDataModel');
const labelMap = {
  0: 'sudden_braking',
  1: 'sharp_turn',
  2: 'inconsistent_speed',
  3: 'normal'
};

async function importCsvToMongoDB(csvPath) {
  const results = [];
  return new Promise((resolve, reject) => {
    fs.createReadStream(csvPath)
      .pipe(csv())
      .on('data', (data) => results.push(data))
      .on('end', async () => {
        const docs = results.map(row => ({
          timestamp: new Date(row.timestamp),
          speed_kmh: parseFloat(row.speed_kmh),
          acceleration_mps2: parseFloat(row.acceleration_mps2),
          steering_angle_deg: parseFloat(row.steering_angle_deg),
          engine_rpm: parseFloat(row.engine_rpm)
        }));
        await SensorData.insertMany(docs);
        resolve();
      })
      .on('error', reject);
  });
}

function labelRow(row, meanSpeed) {
  if (row.acceleration_mps2 <= -3.0) return 0;
  else if (Math.abs(row.steering_angle_deg) >= 25) return 1;
  else if (row.acceleration_mps2 >= 3.0 && row.engine_rpm >= 3500) return 2;
  else return 3;
}

async function classifySensorData() {
  const allData = await SensorData.find({});
  const meanSpeed = allData.reduce((sum, row) => sum + row.speed_kmh, 0) / allData.length;

//    for (const row of allData) {
//      const ml_label = labelRow(row, meanSpeed);
//      const event_label = labelMap[ml_label];
//      await SensorData.updateOne({ _id: row._id }, { ml_label, event_label });
//    }
  const bulkOps = allData.map(row => {
    const ml_label = labelRow(row, meanSpeed);
    const event_label = labelMap[ml_label];
    return {
      updateOne: {
        filter: { _id: row._id },
        update: { ml_label, event_label }
      }
    };
  });

  if (bulkOps.length > 0) {
    await SensorData.bulkWrite(bulkOps);
  }
}

module.exports = { importCsvToMongoDB, classifySensorData };