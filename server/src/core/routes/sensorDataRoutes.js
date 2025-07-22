import express from 'express';
const router = express.Router();
import { importCsvToMongoDB } from '../controllers/sensorDataController.js';

// Example: POST /api/sensor-data/import
router.post('/import', async (req, res) => {
  try {
    await importCsvToMongoDB(req.body.csvPath); // or handle file upload
    res.send('CSV imported');
  } catch (err) {
    res.status(500).send(err.message);
  }
});

export default router;