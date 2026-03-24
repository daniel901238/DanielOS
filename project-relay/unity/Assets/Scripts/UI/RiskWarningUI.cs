using ProjectRelay.Player;
using ProjectRelay.World;
using UnityEngine;
using UnityEngine.UI;

namespace ProjectRelay.UI
{
    public class RiskWarningUI : MonoBehaviour
    {
        [SerializeField] private PlayerTerrainState terrainState;
        [SerializeField] private Image panel;
        [SerializeField] private Text label;

        [Header("Colors")]
        [SerializeField] private Color safeColor = new Color(0.1f, 0.6f, 0.2f, 0.35f);
        [SerializeField] private Color cautionColor = new Color(0.9f, 0.7f, 0.1f, 0.45f);
        [SerializeField] private Color dangerColor = new Color(0.9f, 0.2f, 0.2f, 0.55f);

        private void Awake()
        {
            if (terrainState == null)
                terrainState = FindObjectOfType<PlayerTerrainState>();
        }

        private void OnEnable()
        {
            if (terrainState != null)
                terrainState.RiskLevelChanged += HandleRiskChanged;

            RefreshImmediate();
        }

        private void OnDisable()
        {
            if (terrainState != null)
                terrainState.RiskLevelChanged -= HandleRiskChanged;
        }

        private void RefreshImmediate()
        {
            if (terrainState == null) return;
            HandleRiskChanged(terrainState.CurrentRiskLevel);
        }

        private void HandleRiskChanged(TerrainRiskLevel risk)
        {
            if (panel == null || label == null) return;

            switch (risk)
            {
                case TerrainRiskLevel.Safe:
                    panel.color = safeColor;
                    label.text = "SAFE";
                    break;
                case TerrainRiskLevel.Caution:
                    panel.color = cautionColor;
                    label.text = "CAUTION";
                    break;
                case TerrainRiskLevel.Dangerous:
                    panel.color = dangerColor;
                    label.text = "DANGER";
                    break;
            }
        }
    }
}
