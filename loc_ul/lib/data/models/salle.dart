import 'lieu.dart';

class Salle extends Lieu {
  final String infrastructureId;
  final String capacite;

  Salle({
    super.id = '',
    this.infrastructureId = '',
    super.nom = '',
    super.description = '',
    this.capacite = '',
    super.longitude = '',
    super.latitude = '',
    super.image = '',
    super.situation = '',
    super.type = '',
    super.images = const [],
    super.distance = '',
  });

  factory Salle.fromJson(Map<String, dynamic> json) => Salle(
    id: json['id'] ?? "",
    infrastructureId: json['buildingId'] ?? "",
    nom: json['nom'] ?? "",
    description: json['description'] ?? "",
    capacite: json['capacity'] ?? '',
    longitude: json['longitude']?.toString() ?? "",
    latitude: json['latitude']?.toString() ?? "",
    image: getFirstImage(json['images']),
    situation: json['situation'] ?? "",
    type: json['type'] ?? "",
    images: List<String>.from(json['images'] ?? []),
    distance: json['distance'] ?? "",
  );

  @override
  String toString() {
    return 'Salle(nom: $nom, situation: $situation, image: $image, description: $description, latitude: $latitude, longitude: $longitude, id: $id, infrastructureId: $infrastructureId, capacite: $capacite)';
  }
}

String getFirstImage(dynamic list) {
  if (list is List && list.isNotEmpty) {
    return list.first.toString();
  }
  return '';
}
