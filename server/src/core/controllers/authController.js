import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import User from '../models/userModel.js';
import config from '../../config/index.js';
import admin from '../../config/firebase.js';

const generateToken = (user) => {
  return jwt.sign({ id: user._id }, config.jwtSecret, {
    expiresIn: config.jwtExpiresIn,
  });
};

const toUserResponse = (user) => ({
  id: user._id,
  email: user.email,
  name: user.name,
  profilePictureUrl: user.profilePictureUrl || null,
});

/**
 * @desc Create a new user
 * @route POST /api/auth/signup
 */
const signup = async (req, res) => {
  const { email, name, password } = req.body;
  try {
    const existingUser = await User.findOne({ email });
    if (existingUser) return res.status(400).json({ message: 'Email already exists' });

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = await User.create({ email, name, password: hashedPassword });

    const token = generateToken(newUser);
    res.status(201).json({ token, user: { id: newUser._id, email, name } });

  } catch (err) {
    res.status(500).json({ message: 'Signup failed', error: err.message });
  }
};

/**
 * @desc Existing user can login
 * @route POST /api/auth/login
 */
const login = async (req, res) => {
  const { email, password } = req.body;
  try {
    const user = await User.findOne({ email });
    if (!user) return res.status(400).json({ message: 'Invalid credentials' });

    const isMatch = await bcrypt.compare(password, user.password);
    if (!isMatch) return res.status(400).json({ message: 'Invalid credentials' });

    const token = generateToken(user);
    res.status(200).json({ token, user: { id: user._id, email, name: user.name } });

  } catch (err) {
    res.status(500).json({ message: 'Login failed', error: err.message });
  }
};

/**
 * @desc Upload profile picture; store in Firebase Storage, save URL in MongoDB
 * @route POST /api/auth/me/photo
 */
const uploadProfilePhoto = async (req, res) => {
  if (!req.file) {
    return res.status(400).json({ message: 'No file uploaded' });
  }

  try {
    const userId = req.user._id.toString();
    const bucket = admin.storage().bucket();
    const filename = `profile_pictures/${userId}_${Date.now()}.jpg`;
    const file = bucket.file(filename);

    await file.save(req.file.buffer, {
      metadata: { contentType: req.file.mimetype || 'image/jpeg' },
    });

    const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodeURIComponent(
      filename
    )}?alt=media`;

    await User.findByIdAndUpdate(userId, { profilePictureUrl: publicUrl }, { new: true });
    req.user.profilePictureUrl = publicUrl;

    return res.status(200).json({
      profilePictureUrl: publicUrl,
      user: toUserResponse(req.user),
    });
  } catch (err) {
    console.error('Profile photo upload failed', err);
    res.status(500).json({ message: 'Profile photo upload failed', error: err.message });
  }
};

export { signup, login, uploadProfilePhoto };
