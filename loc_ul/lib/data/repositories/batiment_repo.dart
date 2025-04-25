import 'package:cloud_firestore/cloud_firestore.dart';
import '../local/batiment_local.dart';
import '../models/batiment.dart';

class BatimentRepo {
  final _col = FirebaseFirestore.instance.collection('batiments');

  Future<List<Batiment>> fetchBatiments() async {
    final cache = await BatimentLocal.load();
    if (cache != null && cache.isNotEmpty) {
      _refreshRemote();
      return cache;
    }
    return _refreshRemote();
  }

  Future<List<Batiment>> _refreshRemote() async {
    final snap = await _col.get();
    final bats =
        snap.docs.map((d) {
          final data = d.data();
          data['id'] = d.id; // injecte l’id si absent
          return Batiment.fromJson(data);
        }).toList();
    await BatimentLocal.save(bats);
    return bats;
  }
}
