import tkinter as tk
from tkinter import ttk
import requests
import mysql.connector

# Function to get weather data from the API and save it to a text file and MySQL database
def get_weather():
    city = city_entry.get()
    api_key = "6b0b2fec8690cdf189849d98243514d0"
    url = f"http://api.openweathermap.org/data/2.5/weather?q={city}&appid={api_key}"
    response = requests.get(url)
    data = response.json()

    try:
        # Extract weather information
        temperature = data["main"]["temp"] - 273.15  # Convert temperature from Kelvin to Celsius
        humidity = data["main"]["humidity"]
        wind_speed = data["wind"]["speed"] * 3.6  # Convert wind speed from m/s to km/h
        wind_direction = data["wind"]["deg"]
        pressure = data["main"]["pressure"]
        visibility = data["visibility"]
        precipitation = data.get("rain", {}).get("1h", 0)  # Precipitation in the last 1 hour

        # Display weather information in the labels
        temperature_label.config(text=f"Temperature: {temperature:.2f}°C")
        humidity_label.config(text=f"Humidity: {humidity}%")
        wind_speed_label.config(text=f"Wind Speed: {wind_speed:.2f} km/h")
        wind_direction_label.config(text=f"Wind Direction: {wind_direction}°")
        pressure_label.config(text=f"Atmospheric Pressure: {pressure} hPa")
        visibility_label.config(text=f"Visibility: {visibility} meters")
        precipitation_label.config(text=f"Precipitation (last 1h): {precipitation} mm")

        # Save weather information to a text file
        with open("weather_data.txt", "w") as file:
            file.write(f"City: {city}\n")
            file.write(f"Temperature: {temperature:.2f}°C\n")
            file.write(f"Humidity: {humidity}%\n")
            file.write(f"Wind Speed: {wind_speed:.2f} km/h\n")
            file.write(f"Wind Direction: {wind_direction}°\n")
            file.write(f"Atmospheric Pressure: {pressure} hPa\n")
            file.write(f"Visibility: {visibility} meters\n")
            file.write(f"Precipitation (last 1h): {precipitation} mm\n")

        # Save weather information to the MySQL database
        save_to_database(city, temperature, humidity, wind_speed, wind_direction, pressure, visibility, precipitation)

    except KeyError:
        result_label.config(text="City not found", fg="white", bg="#e74c3c", font=("Helvetica", 18, "bold"))

# Function to save weather information to the MySQL database
def save_to_database(city, temperature, humidity, wind_speed, wind_direction, pressure, visibility, precipitation):
    connection = mysql.connector.connect(
        host="localhost",
        user="root",
        password="1234",
        database="wd"
    )
    cursor = connection.cursor()

    # Create a table if it does not exist
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS weather (
            id INT AUTO_INCREMENT PRIMARY KEY,
            city VARCHAR(255),
            temperature DOUBLE,
            humidity INT,
            wind_speed DOUBLE,
            wind_direction INT,
            pressure INT,
            visibility INT,
            precipitation DOUBLE
        )
    ''')

    # Insert data into the table
    cursor.execute('''
        INSERT INTO weather (city, temperature, humidity, wind_speed, wind_direction, pressure, visibility, precipitation)
        VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
    ''', (city, temperature, humidity, wind_speed, wind_direction, pressure, visibility, precipitation))

    # Commit changes and close the connection
    connection.commit()
    connection.close()

# GUI setup
root = tk.Tk()
root.title("Weather Application")
root.configure(bg="#2c3e50")

style = ttk.Style()
style.configure("TButton", foreground="black", background="#3498db", font=("Helvetica", 16, "bold"), padding=10)
style.map("TButton", foreground=[("pressed", "black"), ("active", "black")], background=[("pressed", "#2980b9"), ("active", "#2980b9")])

city_label = tk.Label(root, text="Enter City:", fg="white", bg="#2c3e50", font=("Helvetica", 20))
city_label.pack(pady=10)

city_entry = ttk.Entry(root, font=("Helvetica", 18))
city_entry.pack(pady=10)

search_button = ttk.Button(root, text="Search", command=get_weather, style="TButton", cursor="hand2")
search_button.pack(pady=20)

result_label = tk.Label(root, text="", bg="#2c3e50", font=("Helvetica", 20))
result_label.pack()

# Weather labels
temperature_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
humidity_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
wind_speed_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
wind_direction_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
pressure_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
visibility_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))
precipitation_label = tk.Label(root, text="", bg="#3498db", font=("Helvetica", 18))

# Pack weather labels
temperature_label.pack()
humidity_label.pack()
wind_speed_label.pack()
wind_direction_label.pack()
pressure_label.pack()
visibility_label.pack()
precipitation_label.pack()

root.mainloop()
