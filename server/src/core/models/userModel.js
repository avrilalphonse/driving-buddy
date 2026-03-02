import mongoose from 'mongoose';

const carDetailsSchema = new mongoose.Schema(
  {
    make: { type: String, default: null },
    model: { type: String, default: null },
    colorName: { type: String, default: null },
    colorHex: { type: String, default: null }
  },
  { _id: false }
);

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true },
  password: { type: String, required: true },
  profilePictureUrl: { type: String, required: false },
  carDetails: { type: carDetailsSchema, required: false, default: null }
});

const User = mongoose.model('User', userSchema);

export default User;
