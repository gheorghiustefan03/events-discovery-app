from collections import defaultdict

INTERACTION_WEIGHTS = {
    'save': 3,
    'unsave': -2,
    'view': 2
}
MIN_CATEGORY_EVENTS = 3


def recommend_event_indices(available_events, event_interactions):

    #Aggregate user interactions by event and category
    event_scores = defaultdict(int)
    category_scores = defaultdict(int)

    for interaction in event_interactions:

        print(interaction)
        weight = INTERACTION_WEIGHTS.get(interaction['InteractionType'], 0)
        event_id = interaction['EventId']
        event_scores[event_id] += weight

    #Link each event to its categories
    event_id_to_categories = {}
    event_id_to_obj = {}

    for event in available_events:
        event_id = event['Id']
        categories = event.get('Categories', [])  #multiple categories
        event_id_to_categories[event_id] = categories
        event_id_to_obj[event_id] = event

    #Build per-category preference based on events the user interacted with
    for event_id, score in event_scores.items():
        categories = event_id_to_categories.get(event_id, [])
        for cat in categories:
            category_scores[cat] += score

    #Count category occurrences in current event list
    category_event_count = defaultdict(int)
    for event in available_events:
        for cat in event.get('Categories', []):
            category_event_count[cat] += 1

    #Compute final scores for available events
    ranked_events = []

    for event in available_events:
        base_score = event_scores.get(event['Id'], 0)
        categories = event.get('Categories', [])
        category_boost = 0

        for cat in categories:
            boost = category_scores.get(cat, 0)
            count = category_event_count.get(cat, 0)
            # Penalize if too few events in this category
            if count < MIN_CATEGORY_EVENTS:
                boost *= 0.2
            category_boost += boost

        total_score = base_score + category_boost
        ranked_events.append((event['Id'], total_score))

    #Sort events by total score descending, then assign index
    ranked_events.sort(key=lambda x: x[1], reverse=True)

    ranked_map = {}
    for idx, (event_id, _) in enumerate(ranked_events):
        ranked_map[event_id] = idx

    return ranked_map

def recommend_top_categories(available_events, event_interactions, top_n=4):
    category_scores = defaultdict(int)
    category_event_count = defaultdict(int)

    # Compute scores based on interactions
    for interaction in event_interactions:
        weight = INTERACTION_WEIGHTS.get(interaction['InteractionType'], 0)
        event_id = interaction['EventId']
        categories = next((e.get('Categories', []) for e in available_events if e['Id'] == event_id), [])
        for cat in categories:
            category_scores[cat] += weight

    # Count how many events per category
    for event in available_events:
        for cat in event.get('Categories', []):
            category_event_count[cat] += 1

    # Adjust score based on event availability
    final_scores = []
    for cat, score in category_scores.items():
        count = category_event_count.get(cat, 0)
        adjusted = score * 0.2 if count < MIN_CATEGORY_EVENTS else score
        final_scores.append((cat, adjusted))

    # Add remaining categories with zero score (for fallback if needed)
    for cat in category_event_count.keys():
        if cat not in category_scores:
            score = 0.2 if category_event_count[cat] < MIN_CATEGORY_EVENTS else 0
            final_scores.append((cat, score))

    # Sort and return top N categories
    final_scores.sort(key=lambda x: x[1], reverse=True)
    top_categories = [cat for cat, _ in final_scores[:top_n]]

    return top_categories