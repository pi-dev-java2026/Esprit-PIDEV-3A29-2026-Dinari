import cv2
import sys
import os
import uuid
import logging
import traceback

# Setup logging
logging.getLogger("deepface").setLevel(logging.ERROR)

# Ensure DeepFace handles TensorFlow messages quietly
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '2'

from deepface import DeepFace

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    try:
        # Configuration
        save_dir = "faces"
        if not os.path.exists(save_dir):
            os.makedirs(save_dir)

        # Initialize Camera
        # Try different backend index based on OS, 0 is default
        cap = cv2.VideoCapture(0)

        if not cap.isOpened():
            print("ERROR: Impossible d'accéder à la caméra.")
            sys.exit(1)

        print("INFO: Appuyez sur la touche ESPACE pour capturer une photo, ou ECHAP pour annuler.")

        captured_frame = None

        while True:
            ret, frame = cap.read()
            if not ret:
                print("ERROR: Échec de lecture de la caméra.")
                break
                
            # Flip the frame horizontally for a mirror effect, more natural
            frame = cv2.flip(frame, 1)

            cv2.imshow('Registration - Appuyez sur Espace pour capturer', frame)

            key = cv2.waitKey(1) & 0xFF
            
            # ESPACE key code is 32
            if key == 32:
                captured_frame = frame
                break
            # ESC key code is 27
            elif key == 27:
                break

        # Release camera and clean up windows
        cap.release()
        cv2.destroyAllWindows()

        if captured_frame is None:
            print("ERROR: Capture annulée par l'utilisateur.")
            sys.exit(0)

        # Save temporarily to analyze
        temp_filename = os.path.join(save_dir, "temp_capture.jpg")
        cv2.imwrite(temp_filename, captured_frame)

        # Detect faces using DeepFace
        try:
            # We use extract_faces to find the number of faces.
            # enforce_detection=False so it doesn't throw exception immediately if no face
            faces = DeepFace.extract_faces(img_path=temp_filename, enforce_detection=False)
            
            # The result is a list of dictionaries if faces are found
            # But if no face is found, usually face recognition model either returns empty or specific dictionary.
            # If enforce_detection=False, it might return the whole image as 1 face with low confidence if no detection explicitly blocks it.
            # A better approach with enforce_detection=True:
            pass
        except Exception as e:
            # If enforce_detection=True and no face is found, it throws ValueError
            pass

        # Let's do it safely
        try:
            faces = DeepFace.extract_faces(img_path=temp_filename, enforce_detection=True)
            num_faces = len(faces)
            
            if num_faces == 0:
                print("ERROR: Aucun visage détecté.")
                os.remove(temp_filename)
            elif num_faces > 1:
                print(f"ERROR: {num_faces} visages détectés. Un seul visage est autorisé.")
                os.remove(temp_filename)
            else:
                # Exactly 1 face
                final_filename = os.path.join(save_dir, f"face_{uuid.uuid4().hex[:8]}.jpg")
                os.rename(temp_filename, final_filename)
                
                # Make path absolute or relative correctly? Depends on Java.
                # Relative to working directory is fine since Java runs it from project root
                print(f"SUCCESS:{final_filename}")

        except ValueError:
            # DeepFace raises ValueError when enforce_detection=True and no face is found
            print("ERROR: Aucun visage détecté sur l'image.")
            if os.path.exists(temp_filename):
                os.remove(temp_filename)
        except Exception as e:
            print(f"ERROR: Erreur lors de l'analyse ({str(e)}).")
            if os.path.exists(temp_filename):
                os.remove(temp_filename)

    except Exception as e:
        print(f"ERROR: Erreur inattendue ({str(e)}).")
        sys.exit(1)

if __name__ == "__main__":
    main()
