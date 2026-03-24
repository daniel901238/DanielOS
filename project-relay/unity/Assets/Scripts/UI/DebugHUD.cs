using ProjectRelay.Gameplay;
using ProjectRelay.Player;
using ProjectRelay.World;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class DebugHUD : MonoBehaviour
    {
        [SerializeField] private PlayerLocomotion locomotion;
        [SerializeField] private LoadoutWeightSystem weight;
        [SerializeField] private PlayerTerrainState terrain;
        [SerializeField] private DeliveryMissionController mission;
        [SerializeField] private Text hudText;

        private void Awake()
        {
            if (locomotion == null) locomotion = FindObjectOfType<PlayerLocomotion>();
            if (weight == null) weight = FindObjectOfType<LoadoutWeightSystem>();
            if (terrain == null) terrain = FindObjectOfType<PlayerTerrainState>();
            if (mission == null) mission = FindObjectOfType<DeliveryMissionController>();
        }

        private void Update()
        {
            if (hudText == null) return;

            string risk = terrain != null ? terrain.CurrentRiskLevel.ToString().ToUpperInvariant() : TerrainRiskLevel.Safe.ToString();
            float stamina = locomotion != null ? locomotion.CurrentStamina : 0f;
            float kg = weight != null ? weight.CarriedWeightKg : 0f;
            float cargo = mission != null ? mission.CargoHealth : 0f;

            hudText.text =
                $"STAMINA: {stamina:0}\n" +
                $"WEIGHT: {kg:0.0}kg\n" +
                $"RISK: {risk}\n" +
                $"CARGO: {cargo:0}%";
        }
    }
}
