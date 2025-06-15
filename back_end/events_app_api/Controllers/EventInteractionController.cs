using events_app_api.Data;
using events_app_api.Models;
using Microsoft.AspNetCore.Mvc;

namespace events_app_api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]

    public class EventInteractionController : ControllerBase
    {
        private readonly ApiContext _context;
        private readonly IWebHostEnvironment _env;
        public EventInteractionController(ApiContext context, IWebHostEnvironment env)
        {
            _context = context;
            _env = env;
        }

        [HttpPost]
        public async Task<IActionResult> RecordInteraction([FromBody] EventInteraction dto)
        {
            var interaction = new EventInteraction
            {
                DeviceId = dto.DeviceId,
                EventId = dto.EventId,
                InteractionType = dto.InteractionType,
                Timestamp = dto.Timestamp
            };
            await _context.EventInteractions.AddAsync(interaction);
            await _context.SaveChangesAsync();
            return Ok();
        }
    }
}
