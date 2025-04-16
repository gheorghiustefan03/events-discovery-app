using Microsoft.AspNetCore.Components.Routing;
using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace events_app_api.Models
{
    public class Event
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        public int Id { get; set; }
        public string? Name { get; set; }
        public string? Description { get; set; }
        public int LocationId { get; set; }
        public Location? Location { get; set; }
        public List<Category> Categories {  get; set; }
        public List<string> ImageUrls { get; set; }
        public string? Link { get; set; }
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
        public Event(int id, string name, string description, Location location, List<Category> categories, List<string> imageUrls, string link, DateTime startDate, DateTime endDate)
        {
            Id = id;
            Name = name;
            Description = description;
            Location = location;
            Categories = categories;
            ImageUrls = imageUrls;
            Link = link;
            StartDate = startDate;
            EndDate = endDate;
        }
        public Event()
        {
            Categories = new List<Category>();
            ImageUrls = new List<string>();
        }
    }
}
