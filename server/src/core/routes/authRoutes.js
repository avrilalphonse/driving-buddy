import express from 'express';
import {
    signup,
    login,
    uploadProfilePhoto
} from '../controllers/authController.js';
import protect from '../middleware/authMiddleware.js';
import { uploadSinglePhoto } from '../middleware/uploadMiddleware.js';

const router = express.Router();

router.post('/signup', signup);
router.post('/login', login);

router.get('/me', protect, (req, res) => {
  const { _id, email, name, profilePictureUrl } = req.user;
  res.status(200).json({
    user: { id: _id, email, name, profilePictureUrl: profilePictureUrl || null },
  });
});

router.post('/me/photo', protect, uploadSinglePhoto, uploadProfilePhoto);

export default router;
