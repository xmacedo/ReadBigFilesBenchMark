## One Billion Row Challenge

### 1. What Is the One Billion Row Challenge?
The challenge is as simple as it is daunting:

You have a dataset containing 1,000,000,000 rows.
Each row includes:
A string identifier (e.g., PROPERTY_A)
A floating-point value (e.g., 123456.78)
You must compute the minimum, average, and maximum price for each unique property.

Example row:

```
PROPERTY_A;123456.78
``` 
By the end, you want a map (or any form of summary) that gives you something like:

```
{
PROPERTY_A=100000.0/175000.0/250000.0,
PROPERTY_B=95000.0/210500.4/399999.9,
...
}
```

The format is Min/Mean/Max for each identifier. The real challenge is doing it fast. Let’s start with the most straightforward method: one thread, reading lines one by one.


