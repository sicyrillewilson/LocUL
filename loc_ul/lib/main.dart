import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:provider/provider.dart';

import 'firebase_options.dart';
import 'ui/main_scaffold.dart';
import 'data/repositories/batiment_repo.dart';
import 'viewmodels/batiment_vm.dart';
import 'data/repositories/infrastructure_repo.dart';
import 'viewmodels/infrastructure_vm.dart';
import 'data/repositories/salle_repo.dart';
import 'viewmodels/map_vm.dart';

/// ---------- 1. point d’entrée ----------
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const Bootstrap()); // <‑‑ on lance le Bootstrap
}

/// ---------- 2. écran de démarrage qui attend Firebase ----------
class Bootstrap extends StatelessWidget {
  const Bootstrap({super.key});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: Firebase.initializeApp(
        options: DefaultFirebaseOptions.currentPlatform,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          // petit loader pendant l'initialisation Firebase
          return const MaterialApp(
            home: Scaffold(body: Center(child: CircularProgressIndicator())),
          );
        }
        // Firebase est prêt : on charge l'application réelle
        return const LocUL();
      },
    );
  }
}

/// ---------- 3. l’application réelle avec MultiProvider ----------
class LocUL extends StatelessWidget {
  const LocUL({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        Provider(create: (_) => BatimentRepo()),
        ChangeNotifierProvider(
          create: (c) => BatimentVM(c.read<BatimentRepo>()),
        ),

        Provider(create: (_) => InfrastructureRepo()),
        ChangeNotifierProvider(
          create: (c) => InfrastructureVM(c.read<InfrastructureRepo>()),
        ),

        Provider(create: (_) => SalleRepo()),
        ChangeNotifierProvider(create: (c) => MapVM(c.read<SalleRepo>())),
        // Ajoute ici d’autres Repos / VMs (Infrastructures, Salles)
      ],
      child: MaterialApp(
        title: 'LocUL',
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
          useMaterial3: true,
        ),
        home: const MainScaffold(),
      ),
    );
  }
}
