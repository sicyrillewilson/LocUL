class Lieu {
  String id;
  String nom;
  String description;
  String longitude;
  String latitude;
  String image;
  String situation;
  String type;
  List<String> images;
  String distance;

  Lieu({
    this.id = "",
    this.nom = "",
    this.description = "",
    this.longitude = "",
    this.latitude = "",
    this.image = "",
    this.situation = "",
    this.type = "",
    this.images = const [],
    this.distance = "",
  });

  Lieu copyWith({
    String? id,
    String? nom,
    String? description,
    String? longitude,
    String? latitude,
    String? image,
    String? situation,
    String? type,
    List<String>? images,
    String? distance,
  }) => Lieu(
    id: id ?? this.id,
    nom: nom ?? this.nom,
    description: description ?? this.description,
    longitude: longitude ?? this.longitude,
    latitude: latitude ?? this.latitude,
    image: image ?? this.image,
    situation: situation ?? this.situation,
    type: type ?? this.type,
    images: images ?? this.images,
    distance: distance ?? this.distance,
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    'nom': nom,
    'description': description,
    'longitude': longitude,
    'latitude': latitude,
    'image': image,
    'situation': situation,
    'type': type,
    'images': images,
    'distance': distance,
  };
}
