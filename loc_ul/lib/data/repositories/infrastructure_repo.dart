import 'package:cloud_firestore/cloud_firestore.dart';
import '../local/infrastructure_local.dart';
import '../models/infrastructure.dart';

class InfrastructureRepo {
  final _col = FirebaseFirestore.instance.collection('infrastructures');

  Future<List<Infrastructure>> fetchInfrastructures() async {
    final cache = await InfrastructureLocal.load();
    if (cache != null && cache.isNotEmpty) {
      _refreshRemote();
      return cache;
    }
    return _refreshRemote();
  }

  Future<List<Infrastructure>> _refreshRemote() async {
    final snap = await _col.get();
    final infrastructures =
        snap.docs.map((d) {
          final data = d.data();
          data['id'] = d.id; // injecte l’id si absent
          return Infrastructure.fromJson(data);
        }).toList();
    await InfrastructureLocal.save(infrastructures);
    return infrastructures;
  }
}
