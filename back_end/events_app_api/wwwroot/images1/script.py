import os

# Path to the parent directory containing the numbered folders
parent_dir = './'  # <- replace this with your path

# List all items in the directory
for folder_name in os.listdir(parent_dir):
    folder_path = os.path.join(parent_dir, folder_name)

    # Check if it's a directory and the name is an integer
    if os.path.isdir(folder_path) and folder_name.isdigit():
        new_folder_name = str(int(folder_name) + 50)
        new_folder_path = os.path.join(parent_dir, new_folder_name)

        # Rename the folder
        os.rename(folder_path, new_folder_path)
        print(f'Renamed: {folder_name} -> {new_folder_name}')
