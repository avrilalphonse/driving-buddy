import mongoose from 'mongoose';

const goalSchema = new mongoose.Schema({
  title: { type: String, required: true },
  progress: { type: Number, default: 0 },
  tips: [String],
  user: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true }
}, { timestamps: true });

goalSchema.index({ user: 1, title: 1 }, { unique: true });

const Goal = mongoose.model('Goal', goalSchema);

export default Goal;