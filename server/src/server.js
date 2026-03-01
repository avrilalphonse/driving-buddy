import express from 'express';
import { createServer } from 'http';
import connectDB from './config/db.js';
import './config/firebase.js';
import config from './config/index.js';
import authRoutes from './core/routes/authRoutes.js';
import sensorDataRoutes from './core/routes/sensorDataRoutes.js';
import goalRoutes from './core/routes/goalRoutes.js';

const app = express();
const server = createServer(app);

app.use(express.json());
connectDB();

// Paths
app.use('/api/auth', authRoutes);
app.use('/api/sensor-data', sensorDataRoutes);
app.use('/api/goals', goalRoutes);

server.listen(config.port, () => {
  console.log(`Server running on port ${config.port}`);
});

server.on('error', (err) => {
  if (err.code === 'EADDRINUSE') {
    console.error(`Port ${config.port} is already in use. Terminate its running process or use a different port.`);
  } else {
    console.error('Server startup error:', err);
  }
});