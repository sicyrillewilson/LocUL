import 'package:cloud_firestore/cloud_firestore.dart';
import '../local/infrastructure_local.dart';
import '../models/infrastructure.dart';
import '../../utils/image_cache_helper.dart';
import 'package:connectivity_plus/connectivity_plus.dart';

class InfrastructureRepo {
  final _col = FirebaseFirestore.instance.collection('infrastructures');

  Future<List<Infrastructure>> fetchInfrastructures() async {
    final cache = await InfrastructureLocal.load();
    final connectivity = await Connectivity().checkConnectivity();

    if (!(connectivity.contains(ConnectivityResult.wifi) ||
        connectivity.contains(ConnectivityResult.mobile))) {
      // Pas de connexion, retour uniquement le cache
      return cache ?? [];
    }

    if (cache != null && cache.isNotEmpty) {
      _refreshRemote(); // rafraîchit en arrière-plan
      return cache;
    }

    return _refreshRemote();
  }

  Future<List<Infrastructure>> _refreshRemote() async {
    final snap = await _col.get();
    final infras = <Infrastructure>[];

    for (var d in snap.docs) {
      final data = d.data();
      data['id'] = d.id;

      final images = List<String>.from(data['images'] ?? []);
      final localImages = <String>[];
      String? localImage;

      for (var imgUrl in images) {
        final localPath = await ImageCacheHelper.downloadAndSaveImage(imgUrl);
        localImages.add(localPath ?? imgUrl); // Fallback si erreur
      }

      data['images'] = localImages;

      if (images.isNotEmpty) {
        localImage = await ImageCacheHelper.downloadAndSaveImage(images.first);
        // fallback si null : utiliser chemin d'une image cassée locale par exemple
        data['image'] = localImage ?? ''; // ← plutôt que images.first
      } else {
        data['image'] = '';
      }

      infras.add(Infrastructure.fromJson(data));
    }

    await InfrastructureLocal.save(infras);
    return infras;
  }
}
