import express from 'express';
const router = express.Router();
import { importCsvToMongoDB, getBucketedData } from '../controllers/sensorDataController.js';
import { get } from 'mongoose';

// Example: POST /api/sensor-data/import
router.post('/import', async (req, res) => {
  try {
    await importCsvToMongoDB(req.body.csvPath); // or handle file upload
    res.send('CSV imported');
  } catch (err) {
    res.status(500).send(err.message);
  }
});

router.get('/get-bucketed-data', getBucketedData)
export default router;