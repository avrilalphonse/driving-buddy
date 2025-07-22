import Goal from '../models/goalModel.js';

const STATIC_TIPS = {
  "Reduce sudden braking": [
    "Ease into the brake",
    "Brake in advance",
    "Maintain safe distance"
  ],
  "Reduce sharp turns": [
    "Slow down before turning",
    "Keep both hands on wheel",
    "Avoid jerky steering movements"
  ],
  "Reduce inconsistent speeds": [
    "Use cruise control where possible",
    "Anticipate traffic flow",
    "Keep steady pressure on gas pedal"
  ],
  "Reduce lane deviation": [
    "Focus on lane markings",
    "Avoid distractions",
    "Use gentle steering corrections"
  ]
};

/**
 * @desc Get all goals for current user
 * @route GET /api/goals
 */
const getGoals = async (req, res) => {
  try {
    const goals = await Goal.find({ user: req.user._id });

    res.json(goals);
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

/**
 * @desc Create a new goal
 * @route POST /api/goals
 */
const createGoal = async (req, res) => {
  try {
    const { title, progress } = req.body;
    const tips = STATIC_TIPS[title] || [];

    const goal = await Goal.create({
      title,
      progress: progress || 0,
      tips,
      user: req.user._id
    });

    res.status(201).json(goal);
  } catch (err) {
    if (err.code === 11000) {
      return res.status(400).json({ message: 'Goal already exists for this user.' });
    }
    res.status(400).json({ message: 'Invalid data', error: err.message });
  }
};

/**
 * @desc Delete a goal by ID
 * @route DELETE /api/goals/:id
 */
const deleteGoal = async (req, res) => {
  try {
    const goal = await Goal.findOneAndDelete({ _id: req.params.id, user: req.user._id });

    if (!goal) {
        return res.status(404).json({ message: 'Goal not found' });
    }

    res.json({ message: 'Goal deleted' });
  } catch (err) {
    res.status(500).json({ message: 'Server error', error: err.message });
  }
};

export { getGoals, createGoal, deleteGoal };