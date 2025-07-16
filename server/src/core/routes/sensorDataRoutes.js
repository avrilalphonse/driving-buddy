const express = require('express');
const router = express.Router();
const { importCsvToMongoDB, classifySensorData } = require('../controllers/sensorDataController');

// Example: POST /api/sensor-data/import
router.post('/import', async (req, res) => {
  try {
    await importCsvToMongoDB(req.body.csvPath); // or handle file upload
    res.send('CSV imported');
  } catch (err) {
    res.status(500).send(err.message);
  }
});

// Example: POST /api/sensor-data/classify
router.post('/classify', async (req, res) => {
  try {
    await classifySensorData();
    res.send('Data classified');
  } catch (err) {
    res.status(500).send(err.message);
  }
});

module.exports = router;