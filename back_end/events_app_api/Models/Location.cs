using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace events_app_api.Models
{
    public class Location
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public string? Name { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }
        public List<Event> Events { get; set; }
        public Location(Location location)
        {
            if(location == null) throw new ArgumentNullException("location");
            Id = location.Id;
            Name = location.Name;
            Latitude = location.Latitude;
            Longitude = location.Longitude;
            Events = location.Events;
        }
        public Location(int id, string name, double latitude, double longitude, List<Event> events)
        {
            Id = id;
            Name = name;
            Latitude = latitude;
            Longitude = longitude;
            Events = events;
        }
        public Location()
        {
            Events = new List<Event>();
        }
        public bool isInCircle(double lat, double lon, long radius)
        {
            const double EarthRadiusKm = 6371.0;
            // Convert latitude and longitude from degrees to radians
            double lat1Rad = ToRadians(Latitude);
            double lon1Rad = ToRadians(Longitude);
            double lat2Rad = ToRadians(lat);
            double lon2Rad = ToRadians(lon);

            // Difference in coordinates
            double deltaLat = lat2Rad - lat1Rad;
            double deltaLon = lon2Rad - lon1Rad;

            // Haversine formula
            double a = Math.Sin(deltaLat / 2) * Math.Sin(deltaLat / 2) +
                       Math.Cos(lat1Rad) * Math.Cos(lat2Rad) *
                       Math.Sin(deltaLon / 2) * Math.Sin(deltaLon / 2);

            double c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));

            // Distance between the two points in kilometers
            double distanceKm = EarthRadiusKm * c;

            // Check if the distance is within the specified radius
            return distanceKm <= (radius / 1000);
        }
        private static double ToRadians(double degrees)
        {
            return degrees * Math.PI / 180.0;
        }
    }
}
