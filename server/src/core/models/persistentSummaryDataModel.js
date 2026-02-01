import mongoose from 'mongoose';

const persistentSummaryDataSchema = new mongoose.Schema({
    tripID: { type: String, required: true, unique: true },
    userID: { type: String, required: true },
    start_of_trip_timestamp: { type: Date, required: true },
    end_of_trip_timestamp: { type: Date, required: true },
    start_of_trip_location: { type: [Number], required: true }, // [longitude, latitude]
    end_of_trip_location: { type: [Number], required: true },   // [longitude, latitude]
    hard_braking: { type: [[Number, Number, Date]], default: [] }, // [[lat, lon, timestamp], ...]
    inconsistent_speed: { type: [[Number, Number, Date]], default: [] },
    lane_deviation: { type: [[Number, Number, Date]], default: [] },
    sharp_turning: { type: [[Number, Number, Date]], default: [] }
}, {
    collection: 'persistent_summary_data',
    timestamps: false
});

const PersistentSummaryData = mongoose.model('PersistentSummaryData', persistentSummaryDataSchema);
export default PersistentSummaryData;
