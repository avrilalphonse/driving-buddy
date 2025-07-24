import express from 'express';
const router = express.Router();
import { importCsvToMongoDB, getSimpleCounts, debugHardBraking, clearAllData } from '../controllers/sensorDataController.js';

// Example: POST /api/sensor-data/import
router.post('/import', async (req, res) => {
  try {
    await importCsvToMongoDB(req.body.csvPath); // or handle file upload
    res.send('CSV imported');
  } catch (err) {
    res.status(500).send(err.message);
  }
});

router.get('/counts', getSimpleCounts);
router.get('/debug-braking', debugHardBraking);
router.delete('/clear', clearAllData);

export default router;