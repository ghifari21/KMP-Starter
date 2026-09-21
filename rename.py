import os
import shutil

OLD_PACKAGE = "com.project.starter"
OLD_APP_NAME = "KMP Starter"
OLD_APP_ID = "com.project.starter"

def rename_package(new_package, new_app_name):
    # 1. Rename files content
    for root, dirs, files in os.walk("."):
        if ".git" in root or "build" in root or ".gradle" in root or "venv" in root:
            continue
        for file in files:
            if file.endswith((".kt", ".kts", ".xml", ".swift", ".md", ".properties", ".java")):
                file_path = os.path.join(root, file)
                try:
                    with open(file_path, "r", encoding="utf-8") as f:
                        content = f.read()
                    
                    new_content = content.replace(OLD_PACKAGE, new_package)
                    new_content = new_content.replace(OLD_APP_NAME, new_app_name)
                    new_content = new_content.replace(OLD_APP_ID, new_package)
                    
                    if content != new_content:
                        with open(file_path, "w", encoding="utf-8") as f:
                            f.write(new_content)
                except Exception as e:
                    pass

    # 2. Rename directories
    old_path_parts = OLD_PACKAGE.split('.')
    new_path_parts = new_package.split('.')
    old_path_suffix = os.sep.join(old_path_parts)
    new_path_suffix = os.sep.join(new_path_parts)

    for root, dirs, files in os.walk(".", topdown=False):
        if ".git" in root or "build" in root or ".gradle" in root:
            continue
        if root.endswith(old_path_suffix):
            new_root = root[:-len(old_path_suffix)] + new_path_suffix
            os.makedirs(os.path.dirname(new_root), exist_ok=True)
            shutil.move(root, new_root)

if __name__ == "__main__":
    print("Welcome to KMP Starter Project Setup!")
    new_pkg = input("Enter new package name (e.g., com.mycompany.app): ").strip()
    new_name = input("Enter new App Name (e.g., My Awesome App): ").strip()
    
    if new_pkg and new_name:
        rename_package(new_pkg, new_name)
        print("Success! Project renamed to", new_pkg)
    else:
        print("Invalid input, aborting.")
