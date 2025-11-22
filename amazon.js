import express from "express";
import fetch from "node-fetch";

const app = express();
app.use(express.json());

// ------------------- AMAZON PA-API CONFIG -------------------
const ACCESS_KEY = "YOUR_AMAZON_ACCESS_KEY";
const SECRET_KEY = "YOUR_AMAZON_SECRET_KEY";
const PARTNER_TAG = "YOUR_AMAZON_ASSOCIATE_TAG";
// -------------------------------------------------------------

// Map ASINs to MobChaser titles
const customTitles = {
  "B0CHX2R8F5": "MobChaser iPhone 15 Pro Max",
  "B09XYZ1234": "MobChaser iPhone 14 Ultra",
  "B0CHX2R8F6": "MobChaser Samsung Galaxy S24",
  "B09XYZ1235": "MobChaser OnePlus 12 Pro"
};

// Mock ASIN list for demo (replace with dynamic fetching from PA-API categories)
const asinList = Object.keys(customTitles);

// Function to fetch Amazon PA-API data (mock, replace with signed request)
async function fetchAmazonPA(asin) {
  return {
    ASIN: asin,
    Title: customTitles[asin],
    Brand: asin.includes("Samsung") ? "Samsung" : asin.includes("OnePlus") ? "OnePlus" : "Apple",
    Images: { Primary: "https://m.media-amazon.com/images/I/71V4+Example.jpg" },
    Features: ["A16 Bionic", "6.1” OLED Display", "48MP Triple Camera"],
    Price: Math.floor(Math.random() * 50000 + 5000) + " ₹",
    URL: `https://www.amazon.in/dp/${asin}?tag=${PARTNER_TAG}`
  };
}

// GET /amazon?asin=XXX or /amazon?all=true
app.get("/amazon", async (req, res) => {
  const { asin, all } = req.query;
  try {
    if (all === "true") {
      const results = [];
      for (const a of asinList) {
        const data = await fetchAmazonPA(a);
        results.push(data);
      }
      return res.json(results);
    } else if (asin) {
      const data = await fetchAmazonPA(asin);
      return res.json(data);
    } else {
      return res.status(400).json({ error: "Provide ASIN or all=true" });
    }
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(3000, () => console.log("MobChaser Amazon API running on port 3000"));
