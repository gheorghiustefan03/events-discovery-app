using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using events_app_api.Data;
using events_app_api.Models;
using Microsoft.Extensions.Logging;
using Microsoft.EntityFrameworkCore;

namespace events_app_api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class LocationController : ControllerBase
    {
        private readonly ApiContext _context;
        public LocationController(ApiContext context)
        {
            _context = context;
        }
        [HttpPost]
        public async Task<IActionResult> CreateLocation(Location @location)
        {
            try
            {
                await _context.Locations.AddAsync(@location);
                await _context.SaveChangesAsync();
                return Ok(@location);
            }
            catch (Exception ex)
            {
                return Problem("Server error");
            }
        }
        [HttpPut("{id}")]
        public async Task <IActionResult> UpdateLocation (int id, Location @location)
        {
            try
            {
                Location? _location = await _context.Locations.FindAsync(id);
                if (_location != null)
                {
                    if(@location.Name != null)
                        _location.Name = @location.Name;
                    if(@location.Latitude != default)
                        _location.Latitude = @location.Latitude;
                    if(@location.Longitude != default)
                        _location.Longitude = @location.Longitude;


                    await _context.SaveChangesAsync();
                    return Ok(_location);
                }
                else return NotFound("Location not found");
            } catch(Exception e)
            {
                return Problem("Server Error");
            }
        }

        [HttpGet("{id}")]
        public async Task<IActionResult> getLocation(int id)
        {
            try
            {
                Location? location = await _context.Locations.FindAsync(id);
                if (location != null)
                    return Ok(location);
                else return NotFound();
            } catch(Exception e)
            {
                return Problem("Server error");
            }
        }
        [HttpDelete("{id}")]
        public async Task<IActionResult> deleteLocation(int id)
        {
            try
            {
                Location? loc = await _context.Locations.FindAsync(id);
                if (loc == null) return NotFound();

                _context.Locations.Remove(loc);
                await _context.SaveChangesAsync();
                return Ok("Delete success");
            }
            catch (Exception ex)
            {
                return Problem("Server error");
            }

        }
        [HttpGet]
        public async Task<IActionResult> getLocations([FromQuery] double userLat, [FromQuery] double userLon, [FromQuery] long radius)
        {
            List<Location> locations = await _context.Locations.ToListAsync();
            return Ok(locations.Where(l => l.isInCircle(userLat, userLon, radius)));
        }
    }
}
