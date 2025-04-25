import 'lieu.dart';

class Batiment extends Lieu {
  final String extraProp;

  Batiment({
    super.id,
    super.nom,
    super.description,
    super.longitude,
    super.latitude,
    super.image,
    super.situation,
    super.type,
    super.images,
    super.distance,
    this.extraProp = "",
  });

  factory Batiment.fromJson(Map<String, dynamic> json) => Batiment(
    id: json['id'] ?? "",
    nom: json['nom'] ?? "",
    description: json['description'] ?? "",
    longitude: json['longitude']?.toString() ?? "",
    latitude: json['latitude']?.toString() ?? "",
    image: getFirstImage(json['images']),
    situation: json['situation'] ?? "",
    type: json['type'] ?? "",
    images: List<String>.from(json['images'] ?? []),
    distance: json['distance'] ?? "",
    extraProp: json['extraProp'] ?? "",
  );

  @override
  String toString() {
    return 'Batiment(nom: $nom, situation: $situation, image: $image, description: $description, latitude: $latitude, longitude: $longitude, id: $id)';
  }
}

String getFirstImage(dynamic list) {
  if (list is List && list.isNotEmpty) {
    return list.first.toString();
  }
  return '';
}
