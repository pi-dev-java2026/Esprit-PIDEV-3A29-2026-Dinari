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
    if len(sys.argv) < 2:
        print("ERROR: Aucun chemin d'image de référence fourni.", flush=True)
        sys.exit(1)

    reference_img_path = sys.argv[1]
    
    if not os.path.exists(reference_img_path):
        print(f"ERROR: L'image de référence est introuvable au chemin : {reference_img_path}", flush=True)
        sys.exit(1)

    try:
        # Initialize Camera
        cap = cv2.VideoCapture(0)

        if not cap.isOpened():
            print("ERROR: Impossible d'accéder à la caméra.", flush=True)
            sys.exit(1)

        print("INFO: Appuyez sur la touche ESPACE pour vous identifier, ou ECHAP pour annuler.", flush=True)

        captured_frame = None

        while True:
            ret, frame = cap.read()
            if not ret:
                print("ERROR: Échec de lecture de la caméra.", flush=True)
                break
                
            # Flip the frame
            frame = cv2.flip(frame, 1)

            cv2.imshow('Login - Appuyez sur Espace pour vous identifier', frame)

            key = cv2.waitKey(1) & 0xFF
            
            if key == 32: # SPACE
                captured_frame = frame
                break
            elif key == 27: # ESC
                break

        # Cleanup
        cap.release()
        cv2.destroyAllWindows()

        if captured_frame is None:
            print("ERROR: Identification annulée par l'utilisateur.", flush=True)
            sys.exit(0)

        # Save temporarily
        temp_dir = "faces"
        if not os.path.exists(temp_dir):
            os.makedirs(temp_dir)
            
        temp_filename = os.path.join(temp_dir, f"temp_verify_{uuid.uuid4().hex[:8]}.jpg")
        cv2.imwrite(temp_filename, captured_frame)

        try:
            # 1. Check if there's exactly 1 face in the new picture before verifying
            faces = DeepFace.extract_faces(img_path=temp_filename, enforce_detection=True, detector_backend='opencv')
            num_faces = len(faces)
            
            if num_faces == 0:
                print("ERROR: Aucun visage détecté.", flush=True)
                os.remove(temp_filename)
                sys.exit(0)
            elif num_faces > 1:
                print(f"ERROR: {num_faces} visages détectés. Un seul visage est autorisé.", flush=True)
                os.remove(temp_filename)
                sys.exit(0)
        except ValueError:
            print("ERROR: Aucun visage détecté sur l'image.", flush=True)
            if os.path.exists(temp_filename):
                os.remove(temp_filename)
            sys.exit(0)
        except Exception as e:
            print(f"ERROR: Erreur lors de l'analyse préalable ({str(e)}).", flush=True)
            if os.path.exists(temp_filename):
                os.remove(temp_filename)
            sys.exit(1)

        # 2. Verify against the reference image using OpenFace
        try:
            result = DeepFace.verify(
                img1_path=reference_img_path,
                img2_path=temp_filename,
                model_name='OpenFace',
                detector_backend='opencv',
                enforce_detection=True
            )
            
            # OpenFace is very strict by default (threshold ~0.10). We add a tolerance margin.
            dist = result.get('distance', 0.0)
            threshold = result.get('threshold', 0.10)
            custom_threshold = threshold + 0.15 # Relaxing threshold significantly for webcam variations

            if result["verified"] or dist <= custom_threshold:
                print("SUCCESS:MATCH", flush=True)
            else:
                print(f"DEBUG: Distance={dist:.4f} > CustomThreshold={custom_threshold:.4f}", flush=True)
                print("ERROR:NOT_MATCH", flush=True)
                
        except Exception as e:
            print(f"ERROR: Échec de la vérification ({str(e)}).", flush=True)

        finally:
            if os.path.exists(temp_filename):
                os.remove(temp_filename)

    except Exception as e:
        print(f"ERROR: Erreur inattendue ({str(e)}).", flush=True)
        sys.exit(1)

if __name__ == "__main__":
    main()
