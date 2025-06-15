using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace events_app_api.Migrations
{
    /// <inheritdoc />
    public partial class ThirdCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_EventInteraction_Events_EventId",
                table: "EventInteraction");

            migrationBuilder.DropPrimaryKey(
                name: "PK_EventInteraction",
                table: "EventInteraction");

            migrationBuilder.RenameTable(
                name: "EventInteraction",
                newName: "EventInteractions");

            migrationBuilder.RenameIndex(
                name: "IX_EventInteraction_EventId",
                table: "EventInteractions",
                newName: "IX_EventInteractions_EventId");

            migrationBuilder.AddPrimaryKey(
                name: "PK_EventInteractions",
                table: "EventInteractions",
                columns: new[] { "DeviceId", "Timestamp" });

            migrationBuilder.AddForeignKey(
                name: "FK_EventInteractions_Events_EventId",
                table: "EventInteractions",
                column: "EventId",
                principalTable: "Events",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_EventInteractions_Events_EventId",
                table: "EventInteractions");

            migrationBuilder.DropPrimaryKey(
                name: "PK_EventInteractions",
                table: "EventInteractions");

            migrationBuilder.RenameTable(
                name: "EventInteractions",
                newName: "EventInteraction");

            migrationBuilder.RenameIndex(
                name: "IX_EventInteractions_EventId",
                table: "EventInteraction",
                newName: "IX_EventInteraction_EventId");

            migrationBuilder.AddPrimaryKey(
                name: "PK_EventInteraction",
                table: "EventInteraction",
                columns: new[] { "DeviceId", "Timestamp" });

            migrationBuilder.AddForeignKey(
                name: "FK_EventInteraction_Events_EventId",
                table: "EventInteraction",
                column: "EventId",
                principalTable: "Events",
                principalColumn: "Id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
