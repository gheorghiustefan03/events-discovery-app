# app.py
from flask import Flask, request, jsonify
from engine import recommend_event_indices, recommend_top_categories

app = Flask(__name__)

@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.get_json()
    events = data['availableEvents']
    interactions = data['eventInteractions']

    rankings = recommend_event_indices(events, interactions)
    return jsonify(rankings)

@app.route('/categories', methods=['POST'])
def categories():
    data = request.get_json()
    events = data['availableEvents']
    interactions = data['eventInteractions']

    categories = recommend_top_categories(events, interactions)
    return jsonify(categories)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)