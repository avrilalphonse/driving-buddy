// this model stores:
// userID: which user these insights belong to
// summary: AI-generated text
// last_trip_timestamp: when their last trip was (to detect new trips)
// generated_at: when the insight was created

import mongoose from 'mongoose';

const userInsightsSchema = new mongoose.Schema({
    userID: {
        type: String,
        required: true,
        unique: true,
        index: true
    },
    summary: {
        type: String,
        required: true
    },
    last_trip_timestamp: {
        type: Date,
        required: true
    },
    generated_at: {
        type: Date,
        default: Date.now
    }
}, {
    collection: 'user_insights'
});

const UserInsights = mongoose.model('UserInsights', userInsightsSchema);
export default UserInsights;
