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
        private List<Category> _categories;
        public List<Category> Categories
        {
            get => new List<Category>(_categories);
            set => _categories = value != null ? new List<Category>(value) : new List<Category>();
        }
        private List<string> _imageUrls = new List<string>();
        public List<string> ImageUrls
        {
            get => new List<string>(_imageUrls);
            set => _imageUrls = value != null ? new List<string>(value) : new List<string>();
        }
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
            Categories = null;
            ImageUrls = null;
        }
    }
}
