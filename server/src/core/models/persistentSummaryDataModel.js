import mongoose from 'mongoose';

const persistentSummaryDataSchema = new mongoose.Schema({
  tripID: { type: String, required: true },
  userID: { type: String, required: true },
  start_of_trip_timestamp: { type: Date, required: true },
  end_of_trip_timestamp: { type: Date, required: true },
  start_of_trip_location: {
    type: [Number], // [longitude, latitude]
    default: [0, 0]
  },
  end_of_trip_location: {
    type: [Number], // [longitude, latitude]
    default: [0, 0]
  },
  // Each infraction is an array of [latitude, longitude, timestamp]
  inconsistent_speed: {
    type: [[mongoose.Schema.Types.Mixed]], // Array of [lat, lon, timestamp]
    default: []
  },
  hard_braking: {
    type: [[mongoose.Schema.Types.Mixed]], // Array of [lat, lon, timestamp]
    default: []
  },
  sharp_turning: {
    type: [[mongoose.Schema.Types.Mixed]], // Array of [lat, lon, timestamp]
    default: []
  },
  lane_deviation: {
    type: [[mongoose.Schema.Types.Mixed]], // Array of [lat, lon, timestamp]
    default: []
  }
}, { collection: 'persistent_summary_data' });

const PersistentSummaryData = mongoose.model('PersistentSummaryData', persistentSummaryDataSchema);
export default PersistentSummaryData;