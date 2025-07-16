const mongoose = require('mongoose');

const sensorDataSchema = new mongoose.Schema({
  timestamp: { type: Date, required: true },
  speed_kmh: { type: Number, required: true },
  acceleration_mps2: { type: Number, required: true },
  steering_angle_deg: { type: Number, required: true },
  engine_rpm: { type: Number, required: true },
  ml_label: { type: Number },
  event_label: { type: String }
});

module.exports = mongoose.model('SensorData', sensorDataSchema);