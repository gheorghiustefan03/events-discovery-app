using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using events_app_api.Models;
using events_app_api.Data;
using System.ComponentModel;
using Microsoft.EntityFrameworkCore;
using System;
using System.IO;

namespace events_app_api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class EventController : ControllerBase
    {
        private readonly ApiContext _context;
        private readonly IWebHostEnvironment _env;
        public EventController(ApiContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }
        [HttpPost]
        public async Task<IActionResult> CreateEvent([FromForm]Event @event, [FromForm]List<IFormFile> files)
        {
            try
            {
                string eventImagesFolder = Path.Combine(_env.WebRootPath, "images");
                if (!Directory.Exists(eventImagesFolder)) Directory.CreateDirectory(eventImagesFolder);
                await _context.Events.AddAsync(@event);
                await _context.SaveChangesAsync();
                string path = Path.Combine(eventImagesFolder, @event.Id.ToString());
                if (Directory.Exists(path))
                {
                    foreach(string existingFile in Directory.GetFiles(path))
                    {
                       System.IO.File.Delete(existingFile);
                    }
                    foreach (string subDirectory in Directory.GetDirectories(path))
                    {
                        Directory.Delete(subDirectory, true);
                    }

                }
                else Directory.CreateDirectory(path);
                List<string> imageUrls = new List<string>();
                for (int i = 0; i < files.Count(); i++)
                {
                    string filename = Path.Combine(path, i.ToString()) + Path.GetExtension(Path.GetFileName(files[i].FileName));
                    using (var stream = new FileStream(filename, FileMode.Create))
                    {
                        await files[i].CopyToAsync(stream);
                    }
                    imageUrls.Add(Path.Join("https://localhost:7295", "images" ,@event.Id.ToString(), i.ToString()) + Path.GetExtension(Path.GetFileName(files[i].FileName)));
                }
                @event.ImageUrls = imageUrls;
                await _context.SaveChangesAsync();

                return Ok(@event);
            }catch (Exception ex)
            {
                return Problem("Server error");
            }
        }
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateEvent(int id, Event @event)
        {
            try
            {
                Event? ev = await _context.Events.FindAsync(id);
                if (ev != null)
                {
                    if(@event.Name != null)
                        ev.Name = @event.Name;
                    if (@event.Description != null)
                        ev.Description = @event.Description;
                    if (@event.LocationId != default)
                        ev.LocationId = @event.LocationId;
                    if (@event.Categories != null)
                        ev.Categories = @event.Categories;
                    if (@event.ImageUrls != null)
                        ev.ImageUrls = @event.ImageUrls;
                    if (@event.Link != null)
                        ev.Link = @event.Link;
                    if (@event.StartDate != default)
                        ev.StartDate = @event.StartDate;
                    if (@event.EndDate != default)
                        ev.EndDate = @event.EndDate;

                    await _context.SaveChangesAsync();
                    return Ok(ev);
                }
                else return NotFound("Event not found");
            } catch(Exception ex)
            {
                return Problem("Server error");
            }
        }
        [HttpGet("{id}")]
        public async Task<IActionResult> getEvent(int id)
        {
            try
            {
                Event? _event = await _context.Events.FindAsync(id);
                if (_event != null)
                    return Ok(_event);
                else return NotFound();
            }
            catch (Exception e)
            {
                return Problem("Server error");
            }
        }
        [HttpDelete("{id}")]
        public async Task<IActionResult> deleteEvent(int id)
        {
            try
            {
                Event? ev = await _context.Events.FindAsync(id);
                if (ev == null) return NotFound();

                _context.Events.Remove(ev);
                await _context.SaveChangesAsync();
                return Ok("Delete success");
            }
            catch(Exception ex)
            {
                return Problem("Server error");
            }

        }
        [HttpGet]
        public async Task<IActionResult> getAllEvents([FromQuery(Name = "lId")] int[] locationIds)
        {
            List<Event> events = await _context.Events.ToListAsync();
            List<int> locationIdsList = new List<int>(locationIds);
            return Ok(events.Where(e => locationIdsList.Find(lId => lId == e.LocationId) != default));
        }
        [HttpGet("categories")]
        public async Task<IActionResult> getRandomCategories()
        {
            var categories = new List<Category>(4);
            Category[] allCategories = (Category[])Enum.GetValues(typeof(Category));
            Random rnd = new Random();
            for(int i = 0; i < 4; i++)
            {
                Category category = allCategories[rnd.Next(0, allCategories.Length)];
                if (!categories.Contains(category)) categories.Add(category);
                else i--;
            }
            return Ok(categories);
        }
    }
}
