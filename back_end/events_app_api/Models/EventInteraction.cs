using System.Collections.Specialized;
using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using Newtonsoft.Json;

namespace events_app_api.Models
{
    public class EventInteraction
    {
        public string? DeviceId { get; set; }
        public int EventId { get; set; }
        public string? InteractionType { get; set; }
        public DateTime Timestamp { get; set; }
        [JsonIgnore]
        public Event? Event;
        public EventInteraction(string? deviceId, string? interactionType, DateTime timestamp)
        {
            DeviceId = deviceId;
            InteractionType = interactionType;
            Timestamp = timestamp;
        }
        public EventInteraction() { }
    }
}
