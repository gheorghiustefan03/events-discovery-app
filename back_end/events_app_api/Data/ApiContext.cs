using Microsoft.EntityFrameworkCore;
using events_app_api.Models;
using Microsoft.EntityFrameworkCore.Migrations.Operations;
using System;

namespace events_app_api.Data
{
    public class ApiContext : DbContext
    {
        private readonly IWebHostEnvironment _env;
        public DbSet<Event> Events { get; set; }
        public DbSet<Location> Locations { get; set; }
        public ApiContext(DbContextOptions<ApiContext> options, IWebHostEnvironment env) : base(options)
        {
            _env = env;
        }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.Entity<Event>()
                .HasOne(e => e.Location)
                .WithMany(l => l.Events)
                .HasForeignKey(e => e.LocationId)
                .OnDelete(DeleteBehavior.Cascade);
        }
        public async override Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
        {
            await HandleFileDeletion();
            return await base.SaveChangesAsync(cancellationToken);
        }

        private async Task HandleFileDeletion()
        {
            var deletedEntities = ChangeTracker.Entries()
                                                 .Where(e => e.State == EntityState.Deleted)
                                                 .ToList();

            foreach (var entry in deletedEntities)
            {
                List<Event> deletedEvents = new List<Event>(1);
                if (entry.Entity is Event @event) deletedEvents.Add(@event);
                else if (entry.Entity is Location location)
                {
                    List<Event> eventsList = await Events.ToListAsync();
                    deletedEvents = eventsList.Where(e => e.LocationId == location.Id).ToList();
                }
                foreach(Event deleted in deletedEvents)
                {
                    string folderPath = Path.Combine(_env.WebRootPath, "images", deleted.Id.ToString());
                    Directory.Delete(folderPath, true);
                }
            }
        }
    }
}
