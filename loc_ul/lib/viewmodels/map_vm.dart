import 'package:flutter/material.dart';
import '../data/models/salle.dart';
import '../data/repositories/salle_repo.dart';

class MapVM extends ChangeNotifier {
  final SalleRepo _repo;
  MapVM(this._repo);

  List<Salle>? _items;
  List<Salle>? get items => _items;
  bool _loading = false;
  bool get loading => _loading;

  Future<void> load() async {
    _loading = true;
    notifyListeners();
    final data = await _repo.fetchSalles();
    _items = data;
    _loading = false;
    notifyListeners();
  }
}
