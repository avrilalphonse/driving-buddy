import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import User from '../models/userModel.js';
import config from '../../config/index.js';

const generateToken = (user) => {
  return jwt.sign({ id: user._id }, config.jwtSecret, {
    expiresIn: config.jwtExpiresIn,
  });
};

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

export { signup, login };
