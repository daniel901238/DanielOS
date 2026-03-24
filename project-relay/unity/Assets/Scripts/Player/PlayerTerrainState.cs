using System;
using ProjectRelay.World;
using UnityEngine;

namespace ProjectRelay.Player
{
    public class PlayerTerrainState : MonoBehaviour
    {
        public event Action<TerrainRiskLevel> RiskLevelChanged;

        public float StaminaDrainMultiplier { get; private set; } = 1f;
        public float SlipChanceBonus { get; private set; } = 0f;
        public TerrainRiskLevel CurrentRiskLevel { get; private set; } = TerrainRiskLevel.Safe;

        private void OnTriggerEnter(Collider other)
        {
            var zone = other.GetComponent<TerrainRiskZone>();
            if (zone == null) return;

            ApplyZone(zone);
        }

        private void OnTriggerStay(Collider other)
        {
            var zone = other.GetComponent<TerrainRiskZone>();
            if (zone == null) return;

            ApplyZone(zone);
        }

        private void OnTriggerExit(Collider other)
        {
            var zone = other.GetComponent<TerrainRiskZone>();
            if (zone == null) return;

            SetState(1f, 0f, TerrainRiskLevel.Safe);
        }

        private void ApplyZone(TerrainRiskZone zone)
        {
            SetState(
                Mathf.Max(0.1f, zone.StaminaDrainMultiplier),
                Mathf.Max(0f, zone.SlipChanceBonus),
                zone.RiskLevel
            );
        }

        private void SetState(float staminaMul, float slipBonus, TerrainRiskLevel riskLevel)
        {
            bool changed = CurrentRiskLevel != riskLevel;

            StaminaDrainMultiplier = staminaMul;
            SlipChanceBonus = slipBonus;
            CurrentRiskLevel = riskLevel;

            if (changed)
                RiskLevelChanged?.Invoke(CurrentRiskLevel);
        }
    }
}
