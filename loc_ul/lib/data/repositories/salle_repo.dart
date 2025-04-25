import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:loc_ul/data/local/salle_local.dart';
import '../models/salle.dart';

class SalleRepo {
  final _col = FirebaseFirestore.instance.collection('salles');

  Future<List<Salle>> fetchSalles() async {
    final cache = await SalleLocal.load();
    if (cache != null && cache.isNotEmpty) {
      _refreshRemote();
      return cache;
    }
    return _refreshRemote();
  }

  Future<List<Salle>> _refreshRemote() async {
    final snap = await _col.get();
    final salles =
        snap.docs.map((d) {
          final data = d.data();
          data['id'] = d.id; // injecte l’id si absent
          return Salle.fromJson(data);
        }).toList();
    await SalleLocal.save(salles);
    return salles;
  }
}
