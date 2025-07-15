import express from 'express';
import { createServer } from 'http';
import connectDB from './config/db.js';
import config from './config/index.js';
import authRoutes from './core/routes/authRoutes.js';

const app = express();
const server = createServer(app);

app.use(express.json());
connectDB();

app.use('/api/auth', authRoutes);

server.listen(config.port, () => {
  console.log(`Server running on port ${config.port}`);
});
