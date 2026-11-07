from flask import Flask, request, jsonify
import ddddocr
import base64
import logging

app = Flask(__name__)
ocr = ddddocr.DdddOcr(show_ad=False)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'service': 'ddddocr-service'})

@app.route('/ocr', methods=['POST'])
def recognize():
    try:
        content_type = request.content_type or ''
        
        if 'application/json' in content_type:
            data = request.json.get('image')
            if not data:
                return jsonify({'error': 'No image data'}), 400
            image_bytes = base64.b64decode(data)
        else:
            if 'image' not in request.files:
                return jsonify({'error': 'No image file'}), 400
            file = request.files['image']
            image_bytes = file.read()
        
        result = ocr.classification(image_bytes)
        logger.info(f"OCR result: {result}")
        return jsonify({'text': result})
        
    except Exception as e:
        logger.error(f"OCR failed: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/ocr/url', methods=['POST'])
def recognize_url():
    import requests
    try:
        url = request.json.get('url')
        if not url:
            return jsonify({'error': 'No url provided'}), 400
        
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        }
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        result = ocr.classification(response.content)
        logger.info(f"OCR result from URL: {result}")
        return jsonify({'text': result})
        
    except Exception as e:
        logger.error(f"OCR from URL failed: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    print("=" * 50)
    print("ddddocr HTTP Service")
    print("=" * 50)
    print("Endpoints:")
    print("  POST /ocr         - Upload image (base64 or file)")
    print("  POST /ocr/url     - Provide image URL")
    print("  GET  /health      - Health check")
    print("=" * 50)
    app.run(host='0.0.0.0', port=8083, debug=False)
