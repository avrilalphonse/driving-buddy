import { GoogleGenerativeAI } from "@google/generative-ai";
import UserInsights from '../models/userInsightsModel.js';
import PersistentSummaryData from '../models/persistentSummaryDataModel.js';
import config from '../../config/index.js';

// initialize gemini
const genAI = new GoogleGenerativeAI(config.geminiApiKey);

export const getAIInsights = async (req, res) => {
    try {
        const { userID } = req.query;

        if (!userID) {
            return res.status(400).json({ error: 'userID is required' });
        }

        console.log(`Fetching AI insights for user: ${userID}`);

        // 1.check if we have cached insights
        let cachedInsights = await UserInsights.findOne({ userID });

        // 2. get latest trip data
        const trips = await PersistentSummaryData.find({ userID })
            .sort({ start_of_trip_timestamp: -1 })
            .limit(10); // last 10 trips

        if (trips.length === 0) {
            return res.json({
                summary: "No trips recorded yet. Start driving to see personalized insights!",
                generated_at: new Date(),
                cached: false
            });
        }

        // 3. get the most recent trip timestamp
        const latestTripTimestamp = trips[0].start_of_trip_timestamp;

        // 4. check if cached insights are still valid
        if (cachedInsights &&
            cachedInsights.last_trip_timestamp >= latestTripTimestamp) {
            console.log('Returning cached insights (no new trips)');
            return res.json({
                summary: cachedInsights.summary,
                generated_at: cachedInsights.generated_at,
                cached: true
            });
        }

        console.log('Generating new insights with Gemini AI...');

        // 5. get all trips for overview stats
        const allTrips = await PersistentSummaryData.find({ userID });

        // calc totals
        const totalTrips = allTrips.length;
        const totalIncidents = allTrips.reduce((sum, trip) => {
            return sum +
                (trip.hard_braking?.length || 0) +
                (trip.inconsistent_speed?.length || 0) +
                (trip.lane_deviation?.length || 0) +
                (trip.sharp_turning?.length || 0);
        }, 0);

        // 6. get recent trip details and calc specific metrics
        const recentTrips = trips.map(trip => ({
            date: trip.start_of_trip_timestamp.toISOString().split('T')[0],
            incidents: {
                hard_braking: trip.hard_braking?.length || 0,
                lane_deviation: trip.lane_deviation?.length || 0,
                sharp_turning: trip.sharp_turning?.length || 0,
                inconsistent_speed: trip.inconsistent_speed?.length || 0
            }
        }));

        let hardBrakingCount = 0;
        let laneDeviations = 0;
        let sharpTurns = 0;
        let inconsistentSpeedCount = 0;

        trips.forEach(trip => {
            hardBrakingCount += trip.hard_braking?.length || 0;
            laneDeviations += trip.lane_deviation?.length || 0;
            sharpTurns += trip.sharp_turning?.length || 0;
            inconsistentSpeedCount += trip.inconsistent_speed?.length || 0;
        });


        // 7. build AI prompt
        const prompt = `You are a professional driving coach analyzing a driver's performance.

Overview:
- Total Trips: ${totalTrips}
- Recent Trips Analyzed: ${trips.length}

Incidents (last ${trips.length} trips):
- Hard Braking: ${hardBrakingCount}
- Lane Deviations: ${laneDeviations}
- Sharp Turns: ${sharpTurns}
- Inconsistent Speed: ${inconsistentSpeedCount}

Provide 2-3 short sentences about their driving behavior. Focus on:
- What they're doing well
- What behavior pattern needs improvement (DON'T mention specific numbers)
- One actionable tip

Be friendly and encouraging. NO FLUFF. Don't cite specific incident counts.`;

        // 8. call gemini API
        const model = genAI.getGenerativeModel({ model: "gemini-2.5-flash" }); // latest stable FREE model available, lol
        const result = await model.generateContent(prompt);
        const aiSummary = result.response.text();

        console.log('gemini AI summary generated:', aiSummary);

        // 9. cache new insights
        if (cachedInsights) {
            // update existing
            cachedInsights.summary = aiSummary;
            cachedInsights.last_trip_timestamp = latestTripTimestamp;
            cachedInsights.generated_at = new Date();
            await cachedInsights.save();
        } else {
            // create new
            await UserInsights.create({
                userID,
                summary: aiSummary,
                last_trip_timestamp: latestTripTimestamp,
                generated_at: new Date()
            });
        }

        res.json({
            summary: aiSummary,
            generated_at: new Date(),
            cached: false
        });

    } catch (error) {
        console.error('Error generating AI insights:', error);

        // fallback: try to return cached insights
        try {
            const cachedInsights = await UserInsights.findOne({ userID: req.query.userID });
            if (cachedInsights) {
                console.log('Returning cached insights due to error');
                return res.json({
                    summary: cachedInsights.summary,
                    generated_at: cachedInsights.generated_at,
                    cached: true,
                    error: 'Generated from cache due to API error'
                });
            }
        } catch (cacheError) {
            console.error('Cache fallback failed');
        }

        // final fallback
        res.status(500).json({
            error: 'Failed to generate insights',
            summary: 'Keep practicing safe driving habits and check back later for personalized insights!'
        });
    }
};