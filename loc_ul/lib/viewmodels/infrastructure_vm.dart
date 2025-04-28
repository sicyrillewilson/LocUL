import 'package:flutter/material.dart';
import '../data/models/infrastructure.dart';
import '../data/repositories/infrastructure_repo.dart';

class InfrastructureVM extends ChangeNotifier {
  final InfrastructureRepo _repo;
  InfrastructureVM(this._repo);

  List<Infrastructure>? _items;
  List<Infrastructure>? get items => _items;
  bool _loading = false;
  bool get loading => _loading;

  Future<void> load() async {
    _loading = true;
    notifyListeners();
    final data = await _repo.fetchInfrastructures();
    _items = data;
    _loading = false;
    notifyListeners();
  }
}
