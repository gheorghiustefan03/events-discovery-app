namespace events_app_api.Models
{
    public class IndexRequest
    {
        public string DeviceId { get; set; }
        public List<int> AvailableEventIds { get; set; }
    }
}
