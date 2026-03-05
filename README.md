# KOSIS ETL Pipeline (Java)

## Overview

This project implements a simple **ETL (Extract–Transform–Load) pipeline** in Java that collects statistical data from the **KOSIS (Statistics Korea) Open API**, processes the raw response, and exports cleaned data into structured JSON format.

The goal of this project is to demonstrate how public API data can be automatically collected, normalized, and prepared for further analysis or visualization.

---

## Features

* Collect statistical data from the **KOSIS Open API**
* Convert raw API responses into structured JSON
* Normalize inconsistent fields and remove unnecessary data
* Export cleaned datasets for downstream analysis
* CLI-based execution for reproducible data collection

---

## Architecture

```
KOSIS API
   │
   ▼
Data Fetcher
   │
   ▼
Raw JSON Data
   │
   ▼
Data Cleaning / Normalization
   │
   ▼
Structured JSON Output
```

---

## Project Structure

```
kosis-etl-java
 ├ src
 │   └ main/java
 │        └ (ETL source code)
 │
 ├ build.gradle
 ├ README.md
 └ out
     └ cleaned-data.json
```

---

## How It Works

### 1. Extract

The program sends a request to the **KOSIS Open API** and retrieves statistical data in JSON format.

### 2. Transform

The raw response often contains unnecessary metadata and inconsistent field naming.
The transformation step:

* removes unused fields
* normalizes field names
* restructures the dataset

### 3. Load

The processed dataset is saved locally as a **clean JSON file** that can be used for analytics or visualization tools.

---

## How to Run

### 1. Set the KOSIS API Key

Before running the program, set your **KOSIS API key** as an environment variable.

Mac / Linux:

```
export KOSIS_API_KEY="YOUR_API_KEY"
```

Windows:

```
setx KOSIS_API_KEY "YOUR_API_KEY"
```

---

### 2. Provide `userStatsId`

You must provide a valid **KOSIS userStatsId** when running the program.

Run the program using Gradle:

```
./gradlew run --args="--userStatsId=YOUR_STATS_ID"
```

Example:

```
./gradlew run --args="--userStatsId=kangdh0430/135/DT_13501N_A120/2/1/20250525213809"
```

---

### 3. Output

The processed data will be saved as a JSON file in the output directory.

Example output:

```json
{
  "date": "2024",
  "region": "서울",
  "crime": 321091,
  "population": 9331828
}
```

---

## Limitations

* The pipeline assumes a consistent response structure from the KOSIS API.
* Some statistical tables may require different parameter combinations.
* Error handling for network failures can be further improved.

---

## Future Improvements

* Add automatic retry logic for API requests
* Support multiple statistical tables
* Add CSV export
* Integrate with visualization tools (e.g., Tableau)

---

## Tech Stack

* Java
* Gradle
* REST API
* JSON Processing

---

## Author

**DaeHyuk Kang**

Computer Science & Software Engineering Student
Interested in backend systems, data pipelines, and service reliability.
