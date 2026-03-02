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
  carDetails: user.carDetails || null
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
    res.status(201).json({ token, user: toUserResponse(newUser) });

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
    res.status(200).json({ token, user: toUserResponse(user) });

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

/**
 * @desc Update current user's name and/or email
 * @route PATCH /api/auth/me
 */
const updateProfile = async (req, res) => {
  const { name, email } = req.body;
  const userId = req.user._id;

  try {
    const updates = {};

    if (name != null && name.trim() !== '') {
      updates.name = name.trim();
    }

    if (email != null && email.trim() !== '') {
      const normalizedEmail = email.trim();
      const existing = await User.findOne({ email: normalizedEmail });

      if (existing && existing._id.toString() !== userId.toString()) {
        return res.status(400).json({ message: 'Email already in use' });
      }

      updates.email = normalizedEmail;
    }

    if (Object.keys(updates).length === 0) {
      return res.status(400).json({ message: 'No valid fields to update' });
    }

    const updated = await User.findByIdAndUpdate(userId, updates, { new: true }).select('-password');
    return res.status(200).json({ user: toUserResponse(updated) });
  } catch (err) {
    res.status(500).json({ message: 'Profile update failed', error: err.message });
  }
};

/**
 * @desc Change current user's password
 * @route POST /api/auth/me/change-password
 */
const changePassword = async (req, res) => {
  const { newPassword } = req.body;
  const userId = req.user._id;

  if (!newPassword) {
    return res.status(400).json({ message: 'New password required' });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ message: 'New password must be at least 6 characters' });
  }

  try {
    const hashedPassword = await bcrypt.hash(newPassword, 10);
    await User.findByIdAndUpdate(userId, { password: hashedPassword });

    const updated = await User.findById(userId).select('-password');
    return res.status(200).json({ user: toUserResponse(updated) });
  } catch (err) {
    res.status(500).json({ message: 'Password change failed', error: err.message });
  }
};

/**
 * @desc Update current user's car details
 * @route PATCH /api/auth/me/car
 */
const updateCarDetails = async (req, res) => {
  const userId = req.user._id;
  const { make, model, colorName, colorHex } = req.body;

  if (!make || !model || !colorName || !colorHex) {
    return res
      .status(400)
      .json({ message: 'make, model, colorName, and colorHex are required' });
  }

  try {
    const updated = await User.findByIdAndUpdate(
      userId,
      { carDetails: { make, model, colorName, colorHex } },
      { new: true }
    ).select('-password');

    return res.status(200).json({ user: toUserResponse(updated) });
  } catch (err) {
    return res
      .status(500)
      .json({ message: 'Car details update failed', error: err.message });
  }
};

export { signup, login, uploadProfilePhoto, updateProfile, changePassword, updateCarDetails };
